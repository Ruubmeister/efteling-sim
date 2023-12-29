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

class StatisticsCard extends React.Component<Props> {

    rideStatusVariant(){

        switch(this.props.ride.status.toLowerCase()) {
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

    rideStatusIcon(){

        switch(this.props.ride.status.toLowerCase()) {
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

    isOpen(){
        return this.props.ride.status.toLowerCase() === "open"
    }

    isClosed(){
        return this.props.ride.status.toLowerCase() === "closed"
    }

    isMaintenance(){
        return this.props.ride.status.toLowerCase() === "maintenance"
    }

    toOpen = () => {
        this.putStatus("Open");
    }

    putStatus(status: string){
        var ride = this.props.ride
        console.info(status)
        //ride.status = status; Todo: Fix
        axios({
            method: 'put',
            url: `http://localhost:3997/api/v1/rides/${this.props.ride.id}/status`,
            data: ride
        });
    }

    toClosed = () => {
        this.putStatus("Closed");
    }

    toMaintenance = () => {
        this.putStatus("Maintenance");
    }

    render() {
        return <Card style={{ "margin": "10px 0"}}>
                    <Card.Body>
                        <Card.Title>
                        <ButtonToolbar aria-label="Toolbar with button groups">
                            {this.props.ride.name}
                            <Dropdown className="ml-auto">
                                <Dropdown.Toggle variant={this.rideStatusVariant()} id="dropdown-basic">
                                    <FontAwesomeIcon icon={ this.rideStatusIcon()} />
                                </Dropdown.Toggle>

                                <Dropdown.Menu>
                                    {!this.isOpen() &&
                                        <Dropdown.Item onClick={this.toOpen}>Openen</Dropdown.Item>
                                    } {!this.isMaintenance() &&
                                    <Dropdown.Item onClick={this.toMaintenance} >Naar onderhoud</Dropdown.Item>
                                    } {!this.isClosed() &&
                                    <Dropdown.Item onClick={this.toClosed}>Sluiten</Dropdown.Item>
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
                                    <td>{this.props.ride.visitorsInLine}</td>
                                    <td>{this.props.ride.visitorsInRide}</td>
                                    <td>{this.props.ride.endTime}</td>
                                </tr>
                            </tbody>
                        </Table>
                    </Card.Body>
                </Card>
    }
}


export default StatisticsCard;