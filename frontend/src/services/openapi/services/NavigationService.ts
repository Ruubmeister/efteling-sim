/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { gridLocationDto } from '../models/gridLocationDto';
import type { navigationRequestDto } from '../models/navigationRequestDto';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class NavigationService {

    /**
     * @param requestBody
     * @returns gridLocationDto Route to destination
     * @throws ApiError
     */
    public static postNavigate(
        requestBody: navigationRequestDto,
    ): CancelablePromise<Array<gridLocationDto>> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/v1/navigate',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

}
