import { useState, useContext } from 'react'
import { Context } from '../App'

import Option from '../components/option'
import Search from '../components/search'
import Title from '../components/title'

import SuppItem from './supp-item'
import AddSuppItem from './add-supp-item'

function populateItems()
{
  var itemList = []
  const itemNum = 25

  for(let a = 1; a <= itemNum; a++)
  {
    itemList.push({
      itemid:a,
      vendorPrice:100,
      vendorid:1000-a
    })
  }

  return itemList
}

function Items() {
  const { page, setPage } = useContext(Context)
  const [ suppItems, setSuppItems ] = useState(populateItems)

  return (
    <>
      <Title>Supplied Items</Title>

      <Search onAddClick={() => setPage(<AddSuppItem/>)}/>
      
      {suppItems.map( elem => 
        <Option 
          text1={elem.itemid}
          className1="col-span-2 text-center"
          className2="hidden"
          onClick={() => setPage(<SuppItem
            itemid={elem.itemid}
            vendorPrice={elem.vendorPrice}
            vendorid={elem.vendorid}
          />)}
        /> 
      )}
    </>
  );
}

export default Items;
