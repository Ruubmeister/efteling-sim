import { OpenAPI, visitorDto } from 'src/services/openapi';
import { VisitorService } from '../../services/openapi';
import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { RootState } from '../store';

type state = {
  status: String
  entities: visitorDto[]
}

const initialState: state = {
  status: "idle",
  entities: []
};

export const fetchVisitors = createAsyncThunk('visitors/fetchAll', async () => {
  OpenAPI.BASE = 'http://localhost:49984';
  return await VisitorService.getAllVisitors();
})

const visitorsSlice = createSlice({
  name: 'visitors',
  initialState,
  reducers: {
    // omit reducer cases
  },
  extraReducers: builder => {
    builder
      .addCase(fetchVisitors.pending, (state, _) => {
        state.status = 'loading'
      })
      .addCase(fetchVisitors.fulfilled, (state, action) => {
        const newEntities: visitorDto[] = []
        action.payload.forEach((visitor: visitorDto) => {
          newEntities.push(visitor)
        })
        state.entities = newEntities
        state.status = 'idle'
      })
  }
})

const getVisitors = (state: RootState) => state.visitorReducer.entities;

const {reducer} = visitorsSlice;

export { getVisitors }

export default reducer;