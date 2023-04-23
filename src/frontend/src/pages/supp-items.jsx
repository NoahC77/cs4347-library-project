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

      <Search onAddClick={() => setPage(<AddSuppItem/>)}/>
      
      {suppItems.map( elem => 
        <div className="w-full flex justify-center">
          <div
            className="
            h-[5vh] w-[70%] bg-[#2288BB] rounded-[5px] mb-[5vh]
            border-[2px] border-black
            text-white text-[3vh] grid grid-cols-3 [cursor:pointer]"
            onClick={() => setPage(<SuppItem
              itemid={elem.itemid}
              vendorPrice={elem.vendorPrice}
              vendorid={elem.vendorid}
            />)}
          >
            <div className="border-r-[2px] border-black text-center">
              Vendor: {elem.vendor_name}
            </div>
            <div className="text-center">
              Item: {elem.item_name}
            </div>
            <div className="border-l-[2px] border-black text-center">
              Quantity: {elem.quantity}
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default Items;
