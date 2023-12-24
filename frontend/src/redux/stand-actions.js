import { SET_STANDS } from "./actionTypes";

export const setStands = content => ({
  type: SET_STANDS,
  payload: {
    content
  }
});
