import pyrebase
from time import sleep
from config.config import config

def updateDataBase():

    firebase = pyrebase.initialize_app(config)
    storage = firebase.storage()
    db = firebase.database()
    storage_index = 0
    realtime_index = 0

    image_path = f"images\image1.jpg"  # Specify the path to the image - Storage
    

    # Upload image to Firebase Storage
    storage.child(f"images/image1.jpg").put(image_path) # Storage

    # Get the access token
    access_token = storage.child("images").child("image1.jpg").get_url(None) #Storage
    print(access_token)

    # Update image1 value in the Firebase Realtime Database
    db.child("images").child("image1").set(access_token) # Realtime Database

    print("Image uploaded to Firebase Storage and database updated successfully!")
