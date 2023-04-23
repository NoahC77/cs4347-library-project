import Field from "./field";
import {useContext, useEffect, useState} from "react";
import {BaseUrl} from "../App";
import axios from "axios";

function EndpointField(props) {
  const baseUrl = useContext(BaseUrl)
  const [options, setOptions] = useState([])

  useEffect(() => {
    axios.get(baseUrl + props.endpoint).then((result) => {
      const data = result.data.map(props.transform);
      setOptions(data)
    }).catch((e) => {
      console.error(e)
    })
  }, [])

  return <Field text1={props.text1} drop={true} editable={true} options={options} onValueChange={props.onValueChange}/>
}

export default EndpointField