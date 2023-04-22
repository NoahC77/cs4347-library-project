import { useState, useContext } from 'react'
import { Context } from '../App'

import Option from '../components/option'
import Search from '../components/search'
import Title from '../components/title'

import Ware from './ware'
import AddWare from './add-ware'

function populateItems()
{
  var itemList = []
  const itemNum = 25

  for(let a = 1; a <= itemNum; a++)
  {
    itemList.push({
      name:`Warehouse ${a}`,
      wareid:a,
      sqft:1000 * a,
      state:'Texas',
      city:'Dallas',
      zip:'77777',
      street:'Big Data Drive',
      apt:''
    })
  }

  return itemList
}

function Wares() {
  const { page, setPage } = useContext(Context)
  const [ wares, setWares ] = useState(populateItems)

  return (
    <>
      <Title>Warehouses</Title>

      <Search onAddClick={() => setPage(<AddWare/>)}/>
      
      {wares.map( elem => 
        <Option 
          text1={elem.name}
          text2={`${elem.city}, ${elem.state}`}
          className2="text-right"
          onClick={() => setPage(<Ware
            name={elem.name}
            wareid={elem.wareid}
            sqft={elem.sqft}
            state={elem.state}
            city={elem.city}
            zip={elem.zip}
            street={elem.street}
            apt={elem.apt}
          />)}
        /> 
      )}
    </>
  );
}

export default Wares;
