
const upperLeftCorner = {
    "lat": 51.65325,
    "lon": 5.04505
}

const bottomRightCorner = {
    "lat": 51.64641,
    "lon": 5.05403
}

const gridYCount = 322;
const gridXCount = 262;

const xStep = bottomRightCorner["lat"] - upperLeftCorner["lat"] / gridXCount;
const yStep = bottomRightCorner["lon"] - upperLeftCorner["lon"] / gridYCount;

const calculateLat = (x: number) => {
    return xStep * x + upperLeftCorner["lat"];
}

const calculateLon = (y: number) => {
    return yStep * y + upperLeftCorner["lon"];
}

export {calculateLat, calculateLon}