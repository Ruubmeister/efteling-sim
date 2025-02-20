import React from 'react';
import Card from "react-bootstrap/Card";
import Table from "react-bootstrap/Table";
import Dropdown from "react-bootstrap/Dropdown";
import ButtonToolbar from "react-bootstrap/ButtonToolbar";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import { faCheck, faWrench, faStop, faQuestion } from '@fortawesome/free-solid-svg-icons';
import axios from 'axios';
import { rideDto } from './services/openapi';

type Props = {
    ride: rideDto
}

export function StatisticsCard(props: Props) {

    const rideStatusVariant = () => {

        switch(props.ride.status.toLowerCase()) {
            case "open":
                return "success";
            case "maintenance":
                return "warning";
            case "closed":
                return "danger";
            default:
                return "failure";
        }
    }

    const rideStatusIcon = () => {

        switch(props.ride.status.toLowerCase()) {
            case "open":
                return faCheck;
            case "maintenance":
                return faWrench;
            case "closed":
                return faStop;
            default:
                return faQuestion;
        }
    }

    const isOpen  = () => {
        return props.ride.status.toLowerCase() === "open"
    }

    const isClosed = () => {
        return props.ride.status.toLowerCase() === "closed"
    }

    const isMaintenance = () => {
        return props.ride.status.toLowerCase() === "maintenance"
    }

    const toOpen = () => {
        putStatus("Open");
    }

    const putStatus = (status: string) => {
        var ride = props.ride
        //ride.status = status; Todo: Fix
        axios({
            method: 'put',
            url: `http://localhost:3997/api/v1/rides/${props.ride.id}/status`,
            data: ride
        });
    }

    const toClosed = () => {
        putStatus("Closed");
    }

    const toMaintenance = () => {
        putStatus("Maintenance");
    }

    return <Card style={{ "margin": "10px 0"}}>
            <Card.Body>
                <Card.Title>
                <ButtonToolbar aria-label="Toolbar with button groups">
                    {props.ride.name}
                    <Dropdown className="ml-auto">
                        <Dropdown.Toggle variant={rideStatusVariant()} id="dropdown-basic">
                            <FontAwesomeIcon icon={ rideStatusIcon()} />
                        </Dropdown.Toggle>

                        <Dropdown.Menu>
                            {!isOpen() &&
                                <Dropdown.Item onClick={toOpen}>Openen</Dropdown.Item>
                            } {!isMaintenance() &&
                            <Dropdown.Item onClick={toMaintenance} >Naar onderhoud</Dropdown.Item>
                            } {!isClosed() &&
                            <Dropdown.Item onClick={toClosed}>Sluiten</Dropdown.Item>
                            }
                        </Dropdown.Menu>
                    </Dropdown>
                    </ButtonToolbar>
                </Card.Title>
                <Table striped bordered hover size="sm">
                    <thead>
                        <tr>
                            <th>In Wachtrij</th>
                            <th>In Attractie</th>
                            <th>Tijd klaar</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>{props.ride.visitorsInLine}</td>
                            <td>{props.ride.visitorsInRide}</td>
                            <td>{props.ride.endTime}</td>
                        </tr>
                    </tbody>
                </Table>
            </Card.Body>
        </Card>

}