import pika 
import json, os, random, time
import boto3
from keras.applications import VGG16
from keras.preprocessing.image import img_to_array
from keras.applications import imagenet_utils
from PIL import Image
import numpy as np
import io

model = None

def load_model():
	global model
	model = VGG16(weights="imagenet")
load_model()

def prepare_image(image, target):
	if image.mode != "RGB":
		image = image.convert("RGB")

	image = image.resize(target)
	image = img_to_array(image)
	image = np.expand_dims(image, axis=0)
	image = imagenet_utils.preprocess_input(image)

	return image

def predict(file):
	data = {"success": False}
	image = Image.open(file)

	image = prepare_image(image, target=(224, 224))

	preds = model.predict(image)
	results = imagenet_utils.decode_predictions(preds)
	data["predictions"] = []

	for (imagenetID, label, prob) in results[0]:
		r = {"label": label, "probability": float(prob)}
		data["predictions"].append(r)

	data["success"] = True

	return json.dumps(data)


def process(key):
	""" Processes the task, we can anything here """
	filename = 'tmp/' + key
	client.download_file(bucket_name, key, filename)
	result = predict(filename)
	return result

def callback(ch, method, properties, body):
	files = json.loads(body.decode())
	print("[task-consumer] Processing: %s" % files)

	results = []
	for file in files:
		results.append(json.loads(process(file)))
	

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
bucket_name = 'photos'

client = boto3.client('s3', aws_access_key_id = AWS_ACCESS_KEY_ID, aws_secret_access_key = AWS_SECRET_ACCESS_KEY, endpoint_url='http://127.0.0.1:9000')


connection = pika.BlockingConnection( pika.ConnectionParameters(host='localhost') )

channel = connection.channel()

channel.queue_declare(queue=os.getenv('TASK_QUEUE'), durable=True)

channel.basic_qos(prefetch_count=1)
channel.basic_consume(on_message_callback=callback, queue=os.getenv('TASK_QUEUE'))

print(" [x] Awaiting RPC requests with tasks...")
channel.start_consuming()