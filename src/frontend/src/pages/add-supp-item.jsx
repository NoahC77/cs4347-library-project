import Option from '../components/option'
import Search from '../components/search'
import Field from '../components/field'
import Submit from '../components/submit'
import Title from '../components/title'

import { useState, createContext, useContext } from 'react'
import {BaseUrl, Context} from '../App'
import axios from "axios";
import {toast} from "react-toastify";

function AddSuppItem(props) {
  const { page, setPage } = useContext(Context)
  const baseUrl = useContext(BaseUrl)
  const [item_id, setItemId] = useState(0)
  const [vendor_id, setVendorId] = useState(0)
  const [vendor_price, setVendorPrice] = useState(0)
  const [quantity, setQuantity] = useState(0)

  async function addSuppliedItem(item){
    try{
      await axios.post(baseUrl+ "/addSuppliedItem",item)
      toast("Success")
    } catch (e) {
      toast.error("Error")
    }
  }

  return (
    <>
      <Title>Add Supplied Item</Title>

      <Field editable={true} text1="Item ID:" text2={item_id} onValueChange={setItemId}/>
      <Field editable={true} text1="Vendor ID:" text2={vendor_id} onValueChange={setVendorId}/>
      <Field editable={true} text1="Vendor Price:" text2={vendor_price} onValueChange={setVendorPrice}/>
      <Field editable={true} text1="Quantity:" text2={quantity} onValueChange={setQuantity}/>

      <Submit onClick={()=>addSuppliedItem({
        item_id,vendor_id,vendor_price,quantity,supplied_item_id:-1
      })}/>
    </>
  );
}

export default AddSuppItem;
