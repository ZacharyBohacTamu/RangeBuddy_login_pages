import range as range
from PIL import Image, ImageFilter


def image_to_BnW(input_file, output_file):
    # Open input image
    input_image = Image.open(input_file)
    # Convert image to Grayscale
    output_image = input_image.convert('L')
    # Convert to binary
    # threshold value for splitting black or white
    threshold = 128
    # Load image to read pixel
    pixel = output_image.load()
    # read through each pixel and change the value
    for x in range(output_image.width):
        for y in range(output_image.height):
            pixel_value = pixel[x, y]

            if pixel_value < threshold:
                pixel[x, y] = (0)  # turns pixel White
            else:
                pixel[x, y] = (255)  # turns pixel Black
    # Blur the image
    output_image = output_image.filter(ImageFilter.GaussianBlur(radius=5))
    # Save image
    output_image.save(output_file)
    # print(f'{input_file} has been converted to black and white')
    # files.download(output_file)