import { OpenAPI, rideDto } from 'src/services/openapi';
import { RideService } from '../../services/openapi';
import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { RootState } from '../store';

type state = {
  status: String
  entities: rideDto[]
}

const initialState: state = {
  status: "idle",
  entities: []
};

export const fetchRides = createAsyncThunk('rides/fetchAll', async () => {
  OpenAPI.BASE = 'http://localhost:49981';
  return await RideService.getAllRides();
})

const ridesSlice = createSlice({
  name: 'rides',
  initialState,
  reducers: {
    // omit reducer cases
  },
  extraReducers: builder => {
    builder
      .addCase(fetchRides.pending, (state, _) => {
        state.status = 'loading'
      })
      .addCase(fetchRides.fulfilled, (state, action) => {
        const newEntities: rideDto[] = []
        action.payload.forEach((ride: rideDto) => {
          newEntities.push(ride)
        })
        state.entities = newEntities
        state.status = 'idle'
      })
  }
})

const getRides = (state: RootState) => state.rideReducer.entities;

const {reducer} = ridesSlice;

export { getRides }

export default reducer;