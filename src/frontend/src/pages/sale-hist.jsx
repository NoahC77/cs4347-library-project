import {useState, useContext} from 'react'
import {Context} from '../App'

import Option from '../components/option'
import Search from '../components/search'
import Title from '../components/title'

import MakeSale from './make-sale'
import ListPage from "../components/list-page";

function populateItems() {
  var itemList = []
  const itemNum = 25

  for (let a = 1; a <= itemNum; a++) {
    itemList.push({
      item: `Item ${a}`,
      itemid: a,
      saleid: 1000 + a,
      date: 'yesterday',
    })
  }

  return itemList
}

function SalesHistory(props) {
  return (<div className="w-full flex gap-[2%] justify-center">
    <div className="
            h-[10vh] w-[70%] bg-[#2288BB] rounded-[5px] mb-[5vh] border-[2px] border-black
            text-white text-[3vh] grid grid-rows-2
          ">
      <div className="grid grid-cols-2">
        <div className="w-full border-r-[2px] border-black pl-[2%]">
          {`Item: ${props.item}`}
        </div>
        <div className="w-full border-black pl-[2%]">
          {`Item ID: ${props.itemid}`}
        </div>
      </div>
      <div className="grid grid-cols-2">
        <div className="w-full border-r-[2px] border-t-[2px] border-t-[2px] border-black pl-[2%]">
          {`Sell Date: ${props.date}`}
        </div>
        <div className="w-full border-t-[2px] border-black pl-[2%]">
          {`Sale ID: ${props.saleid}`}
        </div>
      </div>
    </div>
  </div>);
}

function SaleHist() {
  const {page, setPage} = useContext(Context)
  const [items, setItems] = useState(populateItems)

  return (
    <ListPage
      title="Sale History"
      getEndpoint="/salesHistory"
      searchEndpoint=""
      addPage={<MakeSale/>}
      transform={sale => <SalesHistory
        key={sale.sale_id}
        item={sale.item_name}
        itemid={sale.item_id}
        date={sale.date_sold}
        saleid={sale.sale_id}
      />}
    />
  );
}

export default SaleHist;
