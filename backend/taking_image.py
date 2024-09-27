import cv2
import time

frames = []
#index for camera to use 
cam_index = 0
#initial crop index
x = 0
y = 0
#crop with and height
width = 640
height = 480

# Define the camera as a global variable
global cap
cap = None

def connect_camera(cam_index):
    global cap
    camera_index = cam_index  # change this to the index of your camera
    cap = cv2.VideoCapture(camera_index)
    
    while not cap.isOpened():
        print("Camera not connected. Retrying...")
        cap = cv2.VideoCapture(camera_index)
        cv2.waitKey(1000)
        
    print("Camera connected.")    
    return cap  # return the cap variable


def capture_frames():
    global cap
    frames = []
    start_time = time.time()  # get the start time

    while len(frames) < 10:  # continue capturing frames until we have 10
        ret, frame = cap.read()  # read a frame from the camera
        if ret:  # if the frame is successfully read
            timestamp = time.time() - start_time  # calculate the timestamp
            if timestamp >= len(frames) * 0.5:  # check if it's time to capture the next frame
                frames.append(frame)  # add the frame to the list
                print(f"Captured frame {len(frames)} at timestamp {timestamp:.2f}s")
        else:
            print("Error reading frame from camera!")
            break

    
    cv2.destroyAllWindows()  # close all windows

    return frames


def crop_frames(frames, x, y, width, height):
    for i, frame in enumerate(frames):
        frames[i] = frame[y:y+height, x:x+width]
    return frames


def get_frames():
    global cap
    frames = capture_frames()
    frames = crop_frames(frames, x, y, width, height)
    return frames


def close_camera():
    global cap
    cap.release()  # release the camera


