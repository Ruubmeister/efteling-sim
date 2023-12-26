
import { SET_VISITORS } from "../actionTypes";

const initialState = {
  allIds: [],
  byIds: {}
};

export default function(state = initialState, action) {
  switch (action.type) {
      case SET_VISITORS:
        const { content } = action.payload;
        return {
          ...state,
          allIds: content.map(visitor => visitor.id),
          byIds: content.reduce((a, visitor) => ({...a, [visitor.id]: visitor}), {})
        };
    default:
      return state;
  }
}
