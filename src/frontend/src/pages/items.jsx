import {useState, useContext, useEffect} from 'react'
import {BaseUrl, Context} from '../App'

import Option from '../components/option'
import Search from '../components/search'
import Title from '../components/title'

import Item from './item'
import AddItem from './add-item'

import axios from "axios";

function transformResponse(response) {
  return response.data.map( item => {
    return {
      name:item.item_name,
      stock: item.current_stock,
      itemid: item.item_id,
      sellPrice: item.sell_price,
      minStock: item.minimum_stock_level
    }
  });
}

async function getItems(baseUrl) {
  const response = await axios.get(  baseUrl+"/items");

  return transformResponse(response)
}

function getItemsWithQuery(query) {
  return async (baseUrl) => {
    const response = await axios.put(baseUrl + "/itemSearch",{ query });

    return transformResponse(response)
  }
}

function Items(props) {
  const { page, setPage } = useContext(Context)
  const baseUrl = useContext(BaseUrl)
  const [ items, setItems ] = useState([])

  const getItemsActual = props.getItems === undefined ? getItems : props.getItems
  // populateItems
  useEffect(()=>{
    console.log(props)
    getItemsActual(baseUrl).then(response => {
      setItems(response);
    });
  },[]);

  return (
    <>
      <Title>Items</Title>

      <Search onSearchClick={(text)=> setPage(<Items key={text} getItems={getItemsWithQuery(text)}/>)} onAddClick={() => setPage(<AddItem/>)}/>

      {items.map( (elem,index) =>
        <Option
          key={elem.itemid}
          text1={elem.name}
          text2={elem.stock}
          className2="text-right"
          onClick={() => setPage(<Item
            name={elem.name}
            itemid={elem.itemid}
            stock={elem.stock}
            minStock={elem.minStock}
            sellPrice={elem.sellPrice}
          />)}
        />
      )}
    </>
  );
}

export default Items;
