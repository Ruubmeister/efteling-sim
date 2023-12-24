
import { SET_TALES } from "../actionTypes";

const initialState = {
  allIds: [],
  byIds: {}
};

export default function(state = initialState, action) {
  switch (action.type) {
      case SET_TALES:
        const { content } = action.payload;
        return {
          ...state,
          allIds: content.map(tale => tale.guid),
          byIds: content.reduce((a, tale) => ({...a, [tale.guid]: tale}), {})
        };
    default:
      return state;
  }
}
