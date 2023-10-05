import len as len


def shot_location(coordinates):
    # variables needed to average
    sumX = 0  # sum of X
    sumY = 0  # sum of Y

    # loops through to add up the coordinates
    for x, y in coordinates:
        sumX += x
        sumY += y

    # creates average for both coordinates
    avgX = sumX / len(coordinates)
    avgY = sumY / len(coordinates)

    # print(f"Middle of Shot is ({avgX},{avgY})")

    return avgX, avgY
