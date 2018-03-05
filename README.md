# DDCS

#### Note: If you are attempting to run the executable JAR file on Windows you may need to unblock it. (right click > go to "Properties" >  "General" tab > at the bottom will be "This file came from another computer and might be blocked to help protect this computer." with an "Unlock" checkbox next to it.) Failing to do this will cause the program to be unable to load or save images.

#### Current version requires Windows and the Java SE runtime version 9.

A Java program that will take an image, convert it to a new palette of colors, and apply dithering.

A number of built-in palettes are offered, you may define your own using a .txt file, and there is also an option to calculate an adaptive palette based on the image's existing colors using a method of color quantization.

Some examples of color palettes: https://en.wikipedia.org/wiki/List_of_color_palettes

Matching old colors to colors from a new palette is done through a K-d tree for large palettes and an exhaustive search for small palettes; this provides the best performance for each scenario.

The built in dithers offer a variety of error diffusion and ordered dither options.

A description of dithering, as it relates to images: https://en.wikipedia.org/wiki/Dither#Digital_photography_and_image_processing


## Series of examples of the program's output: 

original image - 25255 colors:  
![](http://i.imgur.com/xAVwUMC.jpg)


|              |no dithering|error diffusion (Floyd-Steinberg)|ordered dither (8x8 matrix)
|:---:|:---:|:---:|:---:|
|1-bit (black and white)|![](http://i.imgur.com/R8bWPA9.png)|![](http://i.imgur.com/RmcQZvW.png)|![](http://i.imgur.com/hwVXAOX.png)|
|6-bit (64 colors)|![](http://i.imgur.com/syg0tTP.png)|![](http://i.imgur.com/zORpVYo.png)|![](http://i.imgur.com/03XABGw.png)|
|custom palette (32 colors)|![](http://i.imgur.com/qoZOuys.png)|![](http://i.imgur.com/1xa9rBT.png)|![](http://i.imgur.com/suYYTv0.png)|
|Adaptive palette (32 colors)|![](http://i.imgur.com/Q3jhIkA.png)|![](http://i.imgur.com/bC04lIv.png)|![](http://i.imgur.com/S34ZtUw.png)|
