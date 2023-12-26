
import { SET_STANDS } from "../actionTypes";

const initialState = {
  allIds: [],
  byIds: {}
};

export default function(state = initialState, action) {
  switch (action.type) {
      case SET_STANDS:
        const { content } = action.payload;
        return {
          ...state,
          allIds: content.map(stand => stand.id),
          byIds: content.reduce((a, stand) => ({...a, [stand.id]: stand}), {})
        };
    default:
      return state;
  }
}
