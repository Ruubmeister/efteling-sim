import { OpenAPI, fairyTaleDto } from 'src/services/openapi';
import { FairyTaleService } from '../../services/openapi';
import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { RootState } from '../store';

type state = {
  status: String
  entities: fairyTaleDto[]
}

const initialState: state = {
  status: "idle",
  entities:  []
};

export const fetchFairyTales = createAsyncThunk('fairyTales/fetchAll', async () => {
  OpenAPI.BASE = 'http://localhost:49980';
  return await FairyTaleService.getAllFairyTales();
})

const fairyTalesSlice = createSlice({
  name: 'fairyTales',
  initialState,
  reducers: {
    // omit reducer cases
  },
  extraReducers: builder => {
    builder
      .addCase(fetchFairyTales.pending, (state, _) => {
        state.status = 'loading'
      })
      .addCase(fetchFairyTales.fulfilled, (state, action) => {
        const newEntities: fairyTaleDto[] = []
        action.payload.forEach((fairyTale: fairyTaleDto) => {
          newEntities.push(fairyTale)
        })
        state.entities = newEntities
        state.status = 'idle'
      })
  }
})

const getFairyTales = (state: RootState) => state.fairyTaleReducer.entities;

const {reducer} = fairyTalesSlice;

export { getFairyTales }

export default reducer;