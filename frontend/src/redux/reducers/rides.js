
import { SET_RIDES } from "../actionTypes";

const initialState = {
  allIds: [],
  byIds: {}
};

export default function(state = initialState, action) {
  switch (action.type) {
      case SET_RIDES:
        const { content } = action.payload;
        return {
          ...state,
          allIds: content.map(ride => ride.guid),
          byIds: content.reduce((a, ride) => ({...a, [ride.guid]: ride}), {})
        };
    default:
      return state;
  }
}
