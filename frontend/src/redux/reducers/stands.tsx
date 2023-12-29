import { OpenAPI, standDto } from 'src/services/openapi';
import { StandService } from '../../services/openapi';
import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { RootState } from '../store';

type state = {
  status: String
  entities: standDto[]
}

const initialState: state = {
  status: "idle",
  entities: []
};

export const fetchStands = createAsyncThunk('stands/fetchAll', async () => {
  OpenAPI.BASE = 'http://localhost:49982';
  return await StandService.getAllStands();
})

const standsSlice = createSlice({
  name: 'stands',
  initialState,
  reducers: {
    // omit reducer cases
  },
  extraReducers: builder => {
    builder
      .addCase(fetchStands.pending, (state, _) => {
        state.status = 'loading'
      })
      .addCase(fetchStands.fulfilled, (state, action) => {
        const newEntities: standDto[] = []
        action.payload.forEach((stand: standDto) => {
          newEntities.push(stand)
        })
        state.entities = newEntities
        state.status = 'idle'
      })
  }
})

const getStands = (state: RootState) => state.standReducer.entities;

const {reducer} = standsSlice;

export { getStands }

export default reducer;