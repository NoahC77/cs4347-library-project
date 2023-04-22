import Option from '../components/option'
import Search from '../components/search'
import Field from '../components/field'
import Submit from '../components/submit'
import Title from '../components/title'
import Delete from '../components/delete'

import { useState, createContext, useContext } from 'react'
import { Context } from '../App'

function Item(props) {
  const { page, setPage } = useContext(Context)
  const [ name, setName] = useState(props.name)
  const [ minStock, setMinStock] = useState(props.minStock)
  const [ price, setPrice] = useState(props.price)

  return (
    <>
      <Title>Item</Title>

      <Field editable={true} text1="Name:" text2={`${name}`} onValueChange={setName}/>
      <Field editable={false} text1="ID:" text2={`${props.itemid}`}/>
      <Field editable={false} text1="Stock:" text2={`${props.stock}`}/>
      <Field editable={true} text1="Minimum Stock:" text2={`${minStock}`} onValueChange={setMinStock}/>
      <Field editable={true} text1="Sell Price:" text2={`${price}`} onValueChange={setPrice}/>

      <Submit onClick={()=>console.log({
          name,minStock,price
      })}/>

      <Delete/>
    </>
  );
}

export default Item;
