import base64
import cv2
import numpy as np

var = """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
npVxAAAAAElFTkSuQmCC"""

def decoder(Encoded):
    decoded_data=base64.b64encode(Encoded)
    np_data=np.fromstring(decoded_data,np.uint8)
    img=cv2.imdecode(np_data,cv2.IMREAD_UNCHANGED)
    print(img.shape[0])

    #cv2.imshow('x', img)
    #cv2.waitkey()
    return img

im = decoder(var)
cv2.imshow('x', im)
cv2.waitKey()


