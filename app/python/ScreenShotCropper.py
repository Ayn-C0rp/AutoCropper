import cv2
import numpy as np


def bin_img(image):
    try:

        img = cv2.imread(image)
        gray_img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        binary_img = cv2.threshold(gray_img, 242, 255, cv2.THRESH_BINARY_INV)[1]
        
        return binary_img
    except:
        gray_img = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        binary_img = cv2.threshold(gray_img, 242, 255, cv2.THRESH_BINARY_INV)[1]
        return binary_img


def Get_Bottom_Y(image):
    im2 = cv2.imread(image)
    if int(im2.shape[0]) == 1520:
        im = cv2.imread('Contour22.jpg')
        x = 100
        Range = 900
        
    elif int(im2.shape[0]) == 1920:
        im = cv2.imread('Wide_ResRef.jpg')
        x = 160
        Range = 1060
    elif int(im2.shape[0]) == 1280:
        im = cv2.imread('1280_Ref.jpg')
        x = 92
        Range = 700
    elif int(im2.shape[0]) == 1640:
        im = cv2.imread('Contour22.jpg')
        x = 100
        Range = 900



    ROI = im[:,:]
    Gray = cv2.cvtColor(ROI, cv2.COLOR_BGR2GRAY)
    
    Gray = Gray/255
    y = im2.shape[0]
    y2 = y - int(im.shape[0])
    x2 = x + int(im.shape[1])
    diff = 0
    lowest = 9999
    lowesty = 0
    lowestROI = None
    for k in range(20):
        y = im2.shape[0]
        y2 = y - int(im.shape[0])
        for i in range(Range):
            ROI1 = im2[y2:y,x:x2]
            Gray_ROI = cv2.cvtColor(ROI1, cv2.COLOR_BGR2GRAY)
            Gray_ROI = Gray_ROI / 255
            diff = 0
            diff = np.sum(abs(Gray_ROI - Gray))
            
          
            

            if(diff < lowest):
                lowest = diff
                lowesty = y2
                lowestROI = Gray_ROI
            
            y = y - 1
            y2 = y2 - 1
        
        x = x + 1
        x2 = x2 + 1
    lowesty-= 37
    return lowesty    


def get_top_y(image, boty):
    im = cv2.imread(image)
    if int(im.shape[0]) == 1520:
        Bump = 300
        Line_Thresh = 648
    elif int(im.shape[0]) == 1640:
        Bump = 300
        Line_Thresh = 648    
    elif int(im.shape[0]) == 1920:
        Bump = 600  
        Line_Thresh = 972
    elif int(im.shape[0]) == 1280:
        Bump = 200
        Line_Thresh = 504  
    cap = bin_img(im)
    laplacian = cv2.Laplacian(cap,cv2.CV_64F)
    cv2.imwrite('laplacian.jpg', laplacian)
    boty = boty - Bump
    laplacian = laplacian/255
    for row in range(boty, 0, -1):
        Sum = np.sum(laplacian[row])
        if Sum >= Line_Thresh:
            
            top_y = row
            return top_y


def main():
    return "Olas Amigos"

def crop(image, name):
    bot_y = Get_Bottom_Y(image)
    top_y = get_top_y(image, bot_y)
    im = cv2.imread(image)
    crop = im[top_y:bot_y, :]
    cv2.imwrite(name, crop)
    return 1
    
