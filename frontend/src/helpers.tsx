
const upperLeftCorner = {
    "lat": 1.0,
    "lon": 1.0
}

const bottomRightCorner = {
    "lat": 1.0,
    "lon": 1.0
}

const gridYCount = 300;
const gridXCount = 300;

const xStep = bottomRightCorner["lat"] - upperLeftCorner["lat"] / gridXCount;
const yStep = bottomRightCorner["lon"] - upperLeftCorner["lon"] / gridYCount;

const calculateLat = (x: number) => {
    return xStep * x + upperLeftCorner["lat"];
}

const calculateLon = (y: number) => {
    return yStep * y + upperLeftCorner["lon"];
}

export {calculateLat, calculateLon}