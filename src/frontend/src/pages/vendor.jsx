import Option from '../components/option'
import Search from '../components/search'
import Field from '../components/field'
import Submit from '../components/submit'
import Title from '../components/title'
import Delete from '../components/delete'

import {useState, createContext, useContext, useEffect} from 'react'
import {BaseUrl, Context} from '../App'
import axios from "axios";
import {toast} from "react-toastify";

function Vendor(props) {
  const baseUrl = useContext(BaseUrl)
  const { page, setPage } = useContext(Context)
  const [name, setName] = useState(props.name)
  const [state, setState] = useState(props.state)
  const [city, setCity] = useState(props.city)
  const [zip, setZip] = useState(props.zip)
  const [street, setStreet] = useState(props.street)
  const [apt, setApt] = useState(props.apt)
  useEffect(() => {
    console.log(props)
  }, [])
  async function submit() {
    try {
      await axios.put(baseUrl + `/vendor/${props.vendorid}`, {
        vendor_id: props.vendorid,
        state,
        city,
        street,
        vendor_name: name,
        zip_code: zip,
        apt_code: apt,
      })
      toast.success("Success")
    } catch (e) {
      toast.error("Error")
    }
  }

  async function deleteVendor() {
    try {
      await axios.delete(baseUrl + `/vendor/${props.vendorid}`)
      toast.success("Success")
    } catch (e) {
      toast.error("Error")
    }
  }

  return (
    <>
      <Title>Vendor</Title>

      <Field editable={true} text1="Name:" text2={name} onValueChange={setName}/>
      <Field editable={false} text1="ID:" text2={`${props.vendorid}`}/>
      <Field editable={true} text1="State:" text2={state} onValueChange={setState}/>
      <Field editable={true} text1="City:" text2={city} onValueChange={setCity}/>
      <Field editable={true} text1="Zip Code:" text2={zip} onValueChange={setZip}/>
      <Field editable={true} text1="Street:" text2={street} onValueChange={setStreet}/>
      <Field editable={true} text1="Apartment:" text2={apt} onValueChange={setApt}/>

      <Submit onClick={submit}/>

      <Delete onClick={deleteVendor}/>
    </>
  );
}

export default Vendor;
