import pika 
import json, os, random
import boto3
from keras.applications import VGG16
from keras.preprocessing.image import img_to_array
from keras.applications import imagenet_utils
from keras.preprocessing.image import ImageDataGenerator
from keras.models import Sequential
from keras.layers import Dropout, Flatten, Dense
from keras import applications
from PIL import Image
import numpy as np
import io
import string

# dimensions of our images.
img_width, img_height = 224, 224

top_model_weights_path = 'bottleneck_fc_model.h5'
nb_train_samples = 600
nb_validation_samples = 150
epochs = 20
batch_size = 15


def save_bottlebeck_features(train_data_dir, validation_data_dir):
    datagen = ImageDataGenerator(rescale=1. / 255)

    # build the VGG16 network
    model = applications.VGG16(include_top=False, weights='imagenet')

    generator = datagen.flow_from_directory(
        train_data_dir,
        target_size=(img_width, img_height),
        batch_size=batch_size,
        class_mode=None,
        shuffle=False)
    bottleneck_features_train = model.predict_generator(
        generator, nb_train_samples // batch_size)
    np.save(open('bottleneck_features_train.npy', 'wb'), bottleneck_features_train)

    generator = datagen.flow_from_directory(
        validation_data_dir,
        target_size=(img_width, img_height),
        batch_size=batch_size,
        class_mode=None,
        shuffle=False)
    bottleneck_features_validation = model.predict_generator(
        generator, nb_validation_samples // batch_size)
    np.save(open('bottleneck_features_validation.npy', 'wb'), bottleneck_features_validation)


def train_top_model():
    base_model = applications.VGG16(include_top=False, weights='imagenet', input_shape=(img_width, img_height, 3))
    train_data = np.load(open('bottleneck_features_train.npy', 'rb'))
    train_labels = np.array([0] * (nb_train_samples // 2) + [1] * (nb_train_samples // 2))


    validation_data = np.load(open('bottleneck_features_validation.npy', 'rb'))
    validation_labels = np.array([0] * (nb_validation_samples // 2) + [1] * (nb_validation_samples // 2))
    

    model = Sequential()
    print(base_model.output_shape, train_data.shape[1:])
    model.add(Flatten(input_shape=train_data.shape[1:]))
    model.add(Dense(256, activation='relu'))
    model.add(Dropout(0.5))
    model.add(Dense(1, activation='sigmoid'))
    model.summary()
    print(model.input_shape)

    model.compile(optimizer='rmsprop',
                  loss='binary_crossentropy', metrics=['acc'])

    model.fit(train_data, train_labels,
              epochs=epochs,
              batch_size=batch_size,
              validation_data=(validation_data, validation_labels))
    model.save("tmp/fc_model.h5")

def process(train_buck, valid_buck):
	""" Processes the task, we can anything here """
	train_data_dir = 'tmp/' + train_buck
	valid_data_dir = 'tmp/' + valid_buck
	#save_bottlebeck_features(train_data_dir, valid_data_dir)
	train_top_model()
	print('Uploading file to S3')
	name = 'model_' + ''.join(random.SystemRandom().choice(string.ascii_uppercase + string.digits) for _ in range(12)) + '.h5'
	client.upload_file('tmp/fc_model.h5', 'models', name)

	return 'http://127.0.0.1:9000/models/' + name

def download_bucket(buck):
	print('Downloading bucket', buck)
	bucket = resource.Bucket(buck)
	for s3_object in bucket.objects.all():
    	# Need to split s3_object.key into path and file name, else it will give error file not found.
		path, filename = os.path.split(s3_object.key)
		dest_pathname = os.path.join('tmp/' + buck, s3_object.key)
		if not os.path.exists(os.path.dirname(dest_pathname)):
			os.makedirs(os.path.dirname(dest_pathname))
		bucket.download_file(s3_object.key, dest_pathname)

def callback(ch, method, properties, body):
	buckets = json.loads(body.decode())
	
	print("[task-consumer] Processing: %s" % buckets)
	train_buck = buckets["train_buck"]
	valid_buck = buckets["valid_buck"]
	results = []
	download_bucket(train_buck)
	download_bucket(valid_buck)
	results.append(process(train_buck, valid_buck))



	response = json.dumps(results)
	print("Sending response", response)
	ch.basic_publish(exchange='',
                     routing_key=properties.reply_to,
                     properties=pika.BasicProperties(correlation_id = \
                                                         properties.correlation_id),
                     body=response)
	ch.basic_ack(delivery_tag = method.delivery_tag)

AWS_ACCESS_KEY_ID = 'EGPPV1FE2MOUOJ6V3R3C'
AWS_SECRET_ACCESS_KEY = 'yVIi+pIlvV5ckTjuzlRj+OW32DGxb8jPRExLL2AM'

client = boto3.client('s3', aws_access_key_id = AWS_ACCESS_KEY_ID, aws_secret_access_key = AWS_SECRET_ACCESS_KEY, endpoint_url='http://127.0.0.1:9000')
resource = boto3.resource('s3', aws_access_key_id = AWS_ACCESS_KEY_ID, aws_secret_access_key = AWS_SECRET_ACCESS_KEY, endpoint_url='http://127.0.0.1:9000')

amqp_uri = json.loads(os.environ['VCAP_SERVICES'])['p-rabbitmq'][0]['credentials']['uri']
connection = pika.BlockingConnection( pika.URLParameters(amqp_uri) )

channel = connection.channel()

channel.queue_declare(queue=os.getenv('TASK_QUEUE'), durable=True)

channel.basic_qos(prefetch_count=1)
channel.basic_consume(on_message_callback=callback, queue=os.getenv('TASK_QUEUE'))

print(" [x] Awaiting RPC requests with tasks...")
channel.start_consuming()