import { SET_VISITORS } from "./actionTypes";

export const setVisitors = content => ({
  type: SET_VISITORS,
  payload: {
    content
  }
});
