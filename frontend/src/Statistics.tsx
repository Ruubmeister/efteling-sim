import React from 'react';
import './App.css';

import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col"
import Card from "react-bootstrap/Card";
import Table from "react-bootstrap/Table";
import { StatisticsCard } from "./StatisticsCard"
import { useAppSelector } from './redux/store';
import { rideDto } from './services/openapi';

  function Statistics(){
    const rides = useAppSelector((state) => state.rideReducer.entities)
    const visitors = useAppSelector((state) => state.visitorReducer.entities)

    const rideStatistics = rides.map((ride: rideDto) => 
        <Col xs={6} md={4} lg={3} key={ride.id}>
            <StatisticsCard ride={ride} />
        </Col>
        )
        return <Container fluid>
        <Row>
        <Col xs={6} md={4} lg={3}>
            <Card style={{ "margin": "10px 0"}}>
                <Card.Body>
                    <Card.Title>Park</Card.Title>
                    <Table striped bordered hover size="sm">
                        <tbody>
                            <tr>
                                <td>Bezoekers in park</td>
                                <td>{visitors.length}</td>
                            </tr>
                        </tbody>
                    </Table>
                </Card.Body>
            </Card>
            </Col>
            {rideStatistics}
        </Row>
    </Container>;
}

export default Statistics;