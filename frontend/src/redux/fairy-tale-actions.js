import { SET_TALES } from "./actionTypes";

export const setFairyTales = content => ({
  type: SET_TALES,
  payload: {
    content
  }
});
