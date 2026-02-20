import { visitorDto } from 'src/services/openapi';
import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import { RootState } from '../store';

type state = {
  status: String
  entities: visitorDto[]
}

const initialState: state = {
  status: "idle",
  entities: []
};

const visitorsSlice = createSlice({
  name: 'visitors',
  initialState,
  reducers: {
    visitorsUpdated: (state, action: PayloadAction<visitorDto[]>) => {
      state.entities = action.payload;
      state.status = 'idle';
    }
  },
})

const getVisitors = (state: RootState) => state.visitorReducer.entities;

const { reducer, actions } = visitorsSlice;
const { visitorsUpdated } = actions;

export { getVisitors, visitorsUpdated };

export default reducer;
