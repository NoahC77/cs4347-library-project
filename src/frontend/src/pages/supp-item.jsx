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

function SuppItem(props) {
  const { page, setPage } = useContext(Context)
  const baseUrl = useContext(BaseUrl)
  const [quantity, setQuantity] = useState(props.quantity)
  const [price, setPrice] = useState(props.vendorPrice)

  async function updateSuppliedItem(item) {
    try {
      await axios.put(baseUrl + `/suppliedItem/${item.supplied_item_id}`, item)
      toast("Success")
    }catch (e) {
      toast.error("Error")
    }
  }

  async function deleteItem(item) {
    try {
      await axios.delete(`${baseUrl}/suppliedItem/${item}`)
      toast("Success")
    } catch (e) {
      toast.error("Error")
    }
  }

  return (
    <>
      <Title>Supplied Item</Title>

      <Field editable={false} text1="Supplied Item ID:" text2={`${props.supplied_item_id}`}/>
      <Field editable={false} text1="Item ID:" text2={`${props.item_id}`}/>
      <Field editable={false} text1="Vendor ID:" text2={`${props.vendorid}`}/>
      <Field editable={true} text1="Quantity:" text2={quantity} onValueChange={setQuantity}/>
      <Field editable={true} text1="Vendor Price:" text2={price} onValueChange={setPrice}/>

      <Submit onClick={()=>updateSuppliedItem({
        vendor_id: parseInt(props.vendorid),
        item_id: parseInt(props.item_id),
        vendor_price: parseInt(price),
        quantity: parseInt(quantity),
        supplied_item_id: parseInt(props.supplied_item_id),
      })}/>

      <Delete onClick={()=>deleteItem(props.supplied_item_id)}/>
    </>
  );
}

export default SuppItem;
