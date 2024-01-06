const calculateLat = (_: number, y: number) => {
    return -0.0000125*y+51.65355;
    //return -0.000012*y+51.6537-0.000001*x;
}

const calculateLon = (x: number, _: number) => {
    return 0.00002*x+5.04475;
}

export {calculateLat, calculateLon}