import { SET_RIDES } from "./actionTypes";

export const setRides = content => ({
  type: SET_RIDES,
  payload: {
    content
  }
});
