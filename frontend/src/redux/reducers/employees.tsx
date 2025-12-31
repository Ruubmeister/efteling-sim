import { OpenAPI, employeeDto } from 'src/services/openapi';
import { EmployeeService } from '../../services/openapi';
import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { RootState } from '../store';

type state = {
  status: String
  entities: employeeDto[]
}

const initialState: state = {
  status: "idle",
  entities: []
};

export const fetchEmployees = createAsyncThunk('employees/fetchAll', async () => {
  OpenAPI.BASE = 'http://localhost:49983';
  return await EmployeeService.getAllEmployees();
})

const employeesSlice = createSlice({
  name: 'employees',
  initialState,
  reducers: {
    // omit reducer cases
  },
  extraReducers: builder => {
    builder
      .addCase(fetchEmployees.pending, (state, _) => {
        state.status = 'loading'
      })
      .addCase(fetchEmployees.fulfilled, (state, action) => {
        const newEntities: employeeDto[] = []
        action.payload.forEach((employee: employeeDto) => {
          newEntities.push(employee)
        })
        state.entities = newEntities
        state.status = 'idle'
      })
  }
})

const getEmployees = (state: RootState) => state.employeeReducer.entities;

const {reducer} = employeesSlice;

export { getEmployees }

export default reducer;