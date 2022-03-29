//    public String generateHistory(Map<Sensor, List<BoundaryReading>> data, Map<Sensor, Map<String, Image>> diagrams) {
    //        final StringBuffer content = new StringBuffer();
    //        final List<BoundaryReading> all = new ArrayList<BoundaryReading>();
            //        for(List<BoundaryReading> each : data.values()) {
                //            all.addAll(each);
                //        }
                //        Collections.sort(all, new Comparator<BoundaryReading>() {
                    //            public int compare(BoundaryReading one, BoundaryReading another) {
                    //                return one.getDay().compareTo(another.getDay());
                    //            }
                    //        });
                    //        if(!data.isEmpty()) {
                    //            final Date periodStart = all.get(0).getDay();
                    //            final Date periodEnd = all.get(all.size()-1).getDay();
                    //            content.append("<h3>Zeitraum von ");
                        //            content.append(PublishUtils.dateFormatter.format(periodStart));
                        //            content.append(" bis ");
                        //            content.append(PublishUtils.dateFormatter.format(periodEnd));
                        //            content.append("</h3>");
                    //            PublishUtils.appendTable(content, data, diagrams, this.sensors);
                    //        } else {
                    //            content.append("<h3>Keine Daten vorhanden</h3>");
                    //        }
                    //        return content.toString();
                    //    }