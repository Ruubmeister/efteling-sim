import React from "react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import { faMusic } from '@fortawesome/free-solid-svg-icons';
import Button from "react-bootstrap/Button";
import axios from 'axios';

interface Props {
  url: string
}

class Music extends React.Component<Props> {
    state = {
      play: false
    }
    audio = new Audio(this.props.url)
  
    componentDidMount() {
      this.audio.addEventListener('ended', () => this.setState({ play: false }));
    }
  
    componentWillUnmount() {
      this.audio.removeEventListener('ended', () => this.setState({ play: false }));  
    }
  
    togglePlay = async () => {
      if(await this.musicIsAvailable()){
        console.info("Song found, playing it")
        this.setState({ play: !this.state.play }, () => {
          this.state.play ? this.audio.play() : this.audio.pause();
        });
      } else {
        console.warn("Could not find song.")
      }
    }

    musicIsAvailable = async () => {
      const result = await axios.get(this.props.url)
      .then(function(){
        return true;
      })
      .catch(function (error) {
        console.error(error);
        if (error.response) {
          console.log(error.response.data);
          console.log(error.response.status);
          console.log(error.response.headers);
        }
        return false;
    });
    return result;
    }
  
    render() {
      return (
        <div className="music-player">
          <Button variant="outline-primary" size="sm" onClick={this.togglePlay}><FontAwesomeIcon icon={faMusic} /> {this.state.play ? 'Pauzeren' : 'Afspelen'}</Button>
        </div>
      );
    }
  }
  
  export default Music;