import {useContext, useEffect, useState} from "react";
import {BaseUrl, Context} from "../App";
import axios from "axios";
import Title from "./title";
import Search from "./search";


/**
 * @typedef {Object} ListPageProps
 * @property {string} title
 * @property {string} getEndpoint
 * @property {string} searchEndpoint
 * @property {(Object)=> JSX.Element} transform
 * @property {() => Object[]} [getEntries]
 * @property {JSX.Element} [addPage]
 */

/**
 * @param {ListPageProps} props
 * @param props
 */
function ListPage(props) {
  const {page, setPage} = useContext(Context)
  const baseUrl = useContext(BaseUrl)
  const [entries, setEntries] = useState([])

  const getEntries = props.getEntries ?? (async () => {
    const response = await axios.get(baseUrl + props.getEndpoint)
    return response.data
  });

  function getEntriesWithQuery(query) {
    return async () => {
      const response = await axios.put(baseUrl + props.searchEndpoint, {query});
      return response.data
    }
  }

  function search(query) {
    return setPage(<ListPage key={query} {...props} getEntries={getEntriesWithQuery(query)}/>)
  }

  useEffect(() => {
    getEntries().then((items) => {
      setEntries(items.map(props.transform))
    })
  },[])

  return (
    <>
      <Title>{props.title}</Title>
      <Search onSearchClick={search} onAddClick={() => setPage(props.addPage)}/>

      {entries}
    </>
  )

}

export default ListPage