import cv2
import numpy as np
import time


def teacher_detection(images, threshold=20):

    # Load YOLOv3 network
    net = cv2.dnn.readNetFromDarknet("config\yolov3.cfg", "config\yolov3.weights")

    # Get the names of all layers in the network
    layer_names = net.getLayerNames()
    output_layer_names = [layer_names[i-1] for i in net.getUnconnectedOutLayers()]

    # Load Coco dataset classes
    with open("config\coco.names", "r") as f:
        classes = [line.strip() for line in f.readlines()]

    # Set the class index for humans
    class_index = classes.index("person")

    teacher_coordinates = []

    for i, image in enumerate(images):
        height, width, _ = image.shape

        # take time to process the image
        start_time = time.time()

        # Perform object detection
        blob = cv2.dnn.blobFromImage(image, 1 / 255.0, (416, 416), swapRB=True, crop=True)
        cv2.waitKey(100)
        net.setInput(blob)
        layer_outputs = net.forward(output_layer_names)

        # Process the outputs
        boxes = []
        confidences = []
        for output in layer_outputs:
            for detection in output:
                scores = detection[5:]
                class_id = np.argmax(scores)
                confidence = scores[class_id]
                if class_id == class_index and confidence > 0.5:
                    center_x = int(detection[0] * width)
                    center_y = int(detection[1] * height)
                    box_width = int(detection[2] * width)
                    box_height = int(detection[3] * height)
                    x = center_x - (box_width // 2)
                    y = center_y - (box_height // 2)
                    boxes.append([x, y, box_width, box_height])
                    confidences.append(float(confidence))

        # Apply non-maximum suppression to remove overlapping detections
        indices = cv2.dnn.NMSBoxes(boxes, confidences, score_threshold=0.5, nms_threshold=0.3)
        indices = indices.flatten() if len(indices) > 0 else []

        # Getting the number of humans detected
        num_humans = len(indices)
        print('Humans Detected in image', i, ':', num_humans)
        end_time = time.time()
        print('Time taken to process image', i, ':', end_time - start_time)
        if num_humans > 0:
            # Get the start and end coordinates along the x-axis for each detected teacher
            for j in indices:
                x0, _, w, _ = boxes[j]
                x1 = x0 + w
                x0 = max(0, x0)
                x1 = min(x1, image.shape[1])
                teacher_coordinates.append((x0, x1 + threshold))

    return teacher_coordinates






































# def teacher_detection(images):
#     teacher_indices = []

#     # Load YOLOv3 network
#     net = cv2.dnn.readNetFromDarknet("yolov3.cfg", "yolov3.weights")

#     # Get the names of all layers in the network
#     layer_names = net.getLayerNames()
#     output_layer_names = []
#     output_layer_names = [layer_names[i-1] for i in net.getUnconnectedOutLayers()]

#     # Load Coco dataset classes
#     with open("coco.names", "r") as f:
#         classes = [line.strip() for line in f.readlines()]

#     # Set the class index for humans
#     class_index = classes.index("person")

#     for i, image in enumerate(images):
#         height, width, _ = image.shape

#         # Perform object detection
#         blob = cv2.dnn.blobFromImage(image, 1 / 255.0, (416, 416), swapRB=True, crop=False)
#         net.setInput(blob)
#         layer_outputs = net.forward(output_layer_names)

#         # Process the outputs
#         boxes = []
#         confidences = []
#         for output in layer_outputs:
#             for detection in output:
#                 scores = detection[5:]
#                 class_id = np.argmax(scores)
#                 confidence = scores[class_id]
#                 if class_id == class_index and confidence > 0.5:
#                     center_x = int(detection[0] * width)
#                     center_y = int(detection[1] * height)
#                     box_width = int(detection[2] * width)
#                     box_height = int(detection[3] * height)
#                     x = center_x - (box_width // 2)
#                     y = center_y - (box_height // 2)
#                     boxes.append([x, y, box_width, box_height])
#                     confidences.append(float(confidence))

#         # Apply non-maximum suppression to remove overlapping detections
#         indices = cv2.dnn.NMSBoxes(boxes, confidences, score_threshold=0.5, nms_threshold=0.3)
#         indices = indices.flatten() if len(indices) > 0 else []

#         # Getting the number of humans detected
#         num_humans = len(indices)
#         print('Humans Detected in image', i, ':', num_humans)

#         if num_humans > 0:
#             teacher_indices.append(i)
#             # Draw bounding boxes around the detected humans
#             for j in indices:
#                 x, y, w, h = boxes[j]
#                 cv2.rectangle(image, (x, y), (x + w, y + h), (0, 255, 0), 2)

#     return images, teacher_indices

