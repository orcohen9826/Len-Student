import os
import cv2
import numpy as np

def stitch_images(images, teacher_coordinates, target=0):
    target, source = find_image_to_stitch(teacher_coordinates, target)
    if source is None or target is None:
        return images

    target_image = images[target]
    source_image = images[source]

    target_image[:, teacher_coordinates[target][0]:teacher_coordinates[target][1]] = source_image[:, teacher_coordinates[target][0]:teacher_coordinates[target][1]]

    return target_image

def display_frames_in_grid(frames, numframes):
    # Create a black canvas to display the grid on
    grid = np.zeros((960, 1280, 3), np.uint8)
    
    # Calculate the height and width of each image in the grid
    h, w = frames[0].shape[:2]
    grid_h, grid_w = grid.shape[:2]
    img_h, img_w = grid_h // 2, grid_w // 5
    
    # Resize each image to fit in the grid and insert it into the canvas
    for i in range(numframes):
        img = cv2.resize(frames[i], (img_w, img_h))
        x, y = (i % 5) * img_w, (i // 5) * img_h
        grid[y:y+img_h, x:x+img_w] = img
    
    # Display the grid
    cv2.imshow("Grid", grid)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

def find_image_to_stitch(teacher_coordinates, source):
    x0, x1 = teacher_coordinates[source]

    for i, tupel in enumerate(teacher_coordinates):
        if i == source:
            continue
        xi, xf = tupel
        if not (x0 < xi < x1) and not (x0 < xf < x1):
            return (source, i)
    return (None, None)

def resize_images(images, target_size):
    resized_images = []

    for image in images:
        # Resize the image
        resized_image = cv2.resize(image, target_size)
        resized_images.append(resized_image)

    return resized_images

def load_images_from_folder(folder):
    images = []
    for filename in os.listdir(folder):
        img_path = os.path.join(folder, filename)
        if os.path.isfile(img_path):
            img = cv2.imread(img_path)
            if img is not None:
                images.append(img)
    images = resize_images(images, (640, 480))

    return images