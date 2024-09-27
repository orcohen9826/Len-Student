import cv2
from detection import teacher_detection
from utilities import load_images_from_folder, stitch_images, display_frames_in_grid
from firebase_updtae import updateDataBase
from taking_image import get_frames, close_camera, connect_camera
import time

def process_image(folder_path):
    # Load array of images
    frames = get_frames()
    #images = load_images_from_folder(folder_path)
    teacher_coordinates = teacher_detection(frames)
    if len(teacher_coordinates) == 0:
        cv2.imwrite("images\image1.jpg", frames[-1])
        updateDataBase()
        return 

    
    print(teacher_coordinates)
    image = stitch_images(frames, teacher_coordinates)
    cv2.imwrite("images\image1.jpg", image)
    updateDataBase()
    #display_frames_in_grid(frames,8)
    #time.sleep(5)
    cv2.imshow('image ', image)
    time.sleep(5)  # Sleep for 10 seconds between each iteration
    cv2.destroyAllWindows()



if __name__ == "__main__":
    connect_camera(0)
    start_time = time.time()
    while time.time() - start_time < 600:  # Run for 10 minutes (600 seconds)
        process_image("images")
        cv2.waitKey(10)
        cv2.destroyAllWindows()
    close_camera()






