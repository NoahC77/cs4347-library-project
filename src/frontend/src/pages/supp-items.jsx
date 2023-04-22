import {useState, useContext, useEffect} from 'react'
import {BaseUrl, Context} from '../App'

import Option from '../components/option'
import Search from '../components/search'
import Title from '../components/title'

import SuppItem from './supp-item'
import AddSuppItem from './add-supp-item'
import axios from "axios";

function populateItems() {
  var itemList = []
  const itemNum = 25

  for (let a = 1; a <= itemNum; a++) {
    itemList.push({
      itemid: a,
      vendorPrice: 100,
      vendorid: 1000 - a
    })
  }

  return itemList
}

function Items(props) {
  const {page, setPage} = useContext(Context)
  const baseUrl = useContext(BaseUrl)
  const [suppItems, setSuppItems] = useState([])

  const getItems = props.getItems ?? (async () => {
    const response = await axios.get(baseUrl + "/suppliedItems")

    return response.data
  })
  function getItemsWithQuery(query) {
    return async () => {
      const response = await axios.put(baseUrl + "/suppliedItemSearch",{ query });

      return response.data
    }
  }


  useEffect(() => {
    getItems().then((items) => {
      setSuppItems(
        items.map((item) => ({
          supplied_item_id: item.suppliedItem.supplied_item_id,
          item_id: item.suppliedItem.item_id,
          vendorPrice: item.suppliedItem.vendor_price,
          vendorid: item.suppliedItem.vendor_id,
          item_name: item.item.item_name,
          vendor_name: item.vendor.vendor_name,
          quantity: item.suppliedItem.quantity
        }))
      )
    })
  }, [])


  return (
    <>
      <Title>Supplied Items</Title>

      <Search onSearchClick={(text)=> setPage(<Items key={text} getItems={getItemsWithQuery(text)}/>)} onAddClick={() => setPage(<AddSuppItem/>)}/>

      {suppItems.map(elem =>
        <Option
          key={elem.supplied_item_id}
          text1={`${elem.vendor_name} : ${elem.item_name} : ${elem.quantity}`}
          className1="col-span-2 text-center"
          className2="hidden"
          onClick={() => setPage(<SuppItem
            supplied_item_id={elem.supplied_item_id}
            item_id={elem.item_id}
            vendorPrice={elem.vendorPrice}
            vendorid={elem.vendorid}
            quantity={elem.quantity}
          />)}
        />
      )}

      <div className="flex gap-[2vw] place-content-center place-items-center pb-[5vw]">
        Page {1} of {10}
        <div className="
          h-[5vh] w-[5vw] bg-[#2288BB] rounded-[5px]
          text-white text-[4vh] leading-none text-center [cursor:pointer]
        ">
          {'>'}
        </div>
      </div>
    </>
  );
}

export default Items;
