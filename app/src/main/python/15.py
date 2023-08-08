import cv2
import numpy as np
import base64


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


def Get_Bottom_Y(image, EncString):
    #im2 = cv2.imread(image)
    im2 = image
    print("Bot fine 1")
    im3 = EncString
    #im3 = decoder(EncString)

    if int(im2.shape[0]) == 1520:

        x = 100
        Range = 900
        
    elif int(im2.shape[0]) == 1920:

        x = 160
        Range = 1060
    elif int(im2.shape[0]) == 1280:

        x = 92
        Range = 700
    elif int(im2.shape[0]) == 1640:

        x = 100
        Range = 900


    print("Bot fine 2")
    print(im3.shape[0])
    ROI = im3[:,:]
    print("Bot fine 3")
    Gray = cv2.cvtColor(ROI, cv2.COLOR_BGR2GRAY)
    
    Gray = Gray/255
    y = im2.shape[0]
    y2 = y - int(im3.shape[0])
    x2 = x + int(im3.shape[1])
    diff = 0
    lowest = 9999
    lowesty = 0
    lowestROI = None
    for k in range(20):
        y = im2.shape[0]
        y2 = y - int(im3.shape[0])
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
    #im = cv2.imread(image)
    im = image
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



def decoder(Encoded):
    decoded_data=base64.b64decode(Encoded)
    np_data=np.fromstring(decoded_data,np.uint8)
    img=cv2.imdecode(np_data,cv2.IMREAD_UNCHANGED)


    #cv2.imshow('x', img)
    #cv2.waitkey()
    return img

def encoder(image):
    string_data  = cv2.imencode('.jpg', image)[1].tostring()
    
    half_encoded_data = base64.b64encode(string_data)
    encoded_data = half_encoded_data.decode()

    return  encoded_data


def crop(image, Encoded, name):

    #Im = decoder(image)
    Im = cv2.imread(image)
    Encoded = cv2.imread(Encoded)
    print("fine")
    bot_y = Get_Bottom_Y(Im, Encoded)
    print("fine")
    top_y = get_top_y(Im, bot_y)
    print("fine")
    crop = Im[top_y:bot_y, :]

    
    return crop

cropped = crop('Bababooie.jpg', 'ref_1520.png', 'x')
cv2.imshow('cropped', cropped)
cv2.waitKey()



