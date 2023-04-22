import Option from '../components/option'
import Search from '../components/search'
import Field from '../components/field'
import Submit from '../components/submit'
import Title from '../components/title'
import Delete from '../components/delete'

import {useState, createContext, useContext} from 'react'
import {BaseUrl, Context} from '../App'
import axios from "axios";
import {toast} from "react-toastify";

function Item(props) {
  const {page, setPage} = useContext(Context)
  const [name, setName] = useState(props.name)
  const [minStock, setMinStock] = useState(props.minStock)
  const [price, setPrice] = useState(props.sellPrice)
  const baseUrl = useContext(BaseUrl)

  async function updateItem(item) {
    try {
      const updateResult = await axios.put(baseUrl + `/item/${item.item_id}`, item)
    }catch (e) {
      toast.error("Error")
    }
  }

  async function deleteItem(item) {
    try {
        await axios.delete(`${baseUrl}/item/${item}`)
        toast("Success")
    } catch (e) {
      toast.error("Error")
    }
  }

  return (
    <>
      <Title>Item</Title>

      <Field editable={true} text1="Name:" text2={`${name}`} onValueChange={setName}/>
      <Field editable={false} text1="ID:" text2={`${props.itemid}`}/>
      <Field editable={false} text1="Stock:" text2={`${props.stock}`}/>
      <Field editable={true} text1="Minimum Stock:" text2={`${minStock}`} onValueChange={setMinStock}/>
      <Field editable={true} text1="Sell Price:" text2={`${price}`} onValueChange={setPrice}/>

      <Submit onClick={() => updateItem({
        item_id: props.itemid,
        item_name: name,
        current_stock: props.stock,
        sell_price: price,
        minimum_stock_level: minStock
      })}/>

      <Delete onClick={()=>deleteItem(props.itemid)}/>
    </>
  );


}

export default Item;
