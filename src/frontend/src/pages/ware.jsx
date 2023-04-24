import Option from '../components/option'
import Search from '../components/search'
import Field from '../components/field'
import Submit from '../components/submit'
import Title from '../components/title'
import Delete from '../components/delete'

import { useState, createContext, useContext } from 'react'
import {BaseUrl, Context} from '../App'
import axios from "axios";
import {toast} from "react-toastify";

function Item(props) {
  const { page, setPage } = useContext(Context)
  const baseUrl = useContext(BaseUrl)
  const [name, setName] = useState(props.name)
  const [sqft, setSqft] = useState(props.sqft)
  const [state, setState] = useState(props.state)
  const [city, setCity] = useState(props.city)
  const [zip, setZip] = useState(props.zip)

  const [street, setStreet] = useState(props.street)
  async function submit() {
    try {
      await axios.put(baseUrl + `/warehouse/${props.wareid}`, {
        ware_id: props.wareid,
        sqft,
        state,
        city,
        street,
        ware_name: name,
      })
      toast.success("Success")
    } catch (e) {
      toast.error("Error")
    }
  }

  async function deleteWare() {
    try {
      await axios.delete(baseUrl + `/warehouse/${props.wareid}`)
      toast.success("Success")
    } catch (e) {
      toast.error("Error")
    }
  }

  return (
    <>
      <Title>Warehouse</Title>

      <Field editable={true} text1="Name:" text2={name} onValueChange={setName}/>
      <Field editable={false} text1="ID:" text2={`${props.wareid}`}/>
      <Field editable={true} text1="Square Footage:" text2={sqft} onValueChange={setSqft}/>
      <Field editable={true} text1="State:" text2={state} onValueChange={setState}/>
      <Field editable={true} text1="City:" text2={city} onValueChange={setCity}/>
      <Field editable={true} text1="Street:" text2={street} onValueChange={setStreet}/>
      <Submit onClick={submit}/>

      <Delete onClick={deleteWare}/>
    </>
  );
}

export default Item;
