import Option from '../components/option'
import Search from '../components/search'
import Field from '../components/field'
import Submit from '../components/submit'
import Title from '../components/title'

import { useState, createContext, useContext } from 'react'
import {BaseUrl, Context} from '../App'
import axios from "axios";
import {toast} from "react-toastify";

function AddItem(props) {
  const { page, setPage } = useContext(Context)
  const baseUrl = useContext(BaseUrl)
  const [ name, setName] = useState("")
  const [ minimumStock, setMinimumStock] = useState(0)
  const [ sellPrice, setSellPrice] = useState(0)

  async function addItem(item){
    try{
      console.log(item)
      const result = await axios.post(baseUrl+ "/addItem",item)
      toast("Success")
    } catch (e) {
      toast.error("Error")
    }
  }

  return (
    <>
      <Title>Add Item</Title>

      <Field editable={true} text1="Name:" text2={name} onValueChange={setName}/>
      {/*<Field editable={true} text1="ID:" text2={`${props.itemid}`}/>*/}
      {/*<Field editable={true} text1="Stock:" text2={0}/>*/}
      <Field editable={true} text1="Minimum Stock:" text2={minimumStock} onValueChange={setMinimumStock}/>
      <Field editable={true} text1="Sell Price:" text2={sellPrice} onValueChange={setSellPrice}/>

      <Submit onClick={()=>addItem({
        item_id:-1,
        current_stock:0,
        item_name:name,
        sell_price: parseInt(sellPrice),
        minimum_stock_level: parseInt(minimumStock),
      })}/>
    </>
  );
}

export default AddItem;
