import React, { Component } from 'react'
import axios from 'axios'

class PButton extends Component {
    constructor(props) {
        super(props)

        this.state = {
            session : props.session
        }

        this.joinChat = this.joinChat.bind(this)
    }

    joinChat() {
        const chatId = this.state.session.id
        // const userId = []
        console.log("joinChat()")
        axios.post(`http://localhost:8080/joinChat`, {chatId, "userId": "user1"}).then(r => console.log(r))
    }

    render() {
        return (
            <div>
                <button className='button' onClick={() => this.joinChat()}>
                    {this.state.session.title}
                </button>
            </div>
        )
    }
}

export default PButton