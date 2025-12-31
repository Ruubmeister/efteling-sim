/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { employeeDto } from '../models/employeeDto';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class EmployeeService {
    /**
     * @returns employeeDto A JSON array of employees
     * @throws ApiError
     */
    public static getAllEmployees(): CancelablePromise<Array<employeeDto>> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/employees',
        });
    }
    /**
     * @param id
     * @returns employeeDto The employee that is requested
     * @throws ApiError
     */
    public static getEmployee(
        id: string,
    ): CancelablePromise<employeeDto> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/employees/{id}',
            path: {
                'id': id,
            },
        });
    }
}
