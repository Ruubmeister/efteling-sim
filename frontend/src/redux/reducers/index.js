import { combineReducers } from "redux";
import fairyTales from "./fairy-tales";
import rides from "./rides";
import stands from './stands';
import visitors from "./visitors";

export default combineReducers({ fairyTales, rides, stands, visitors });
