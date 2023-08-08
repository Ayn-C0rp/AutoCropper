import cv2
f = open('stupid_idea.txt', "a")
im = cv2.imread('1520_Ref.jpg');
for row in im:
    f.write(str(im))

f.close()
